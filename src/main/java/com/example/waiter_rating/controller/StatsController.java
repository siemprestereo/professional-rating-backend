package com.example.waiter_rating.controller;

import com.example.waiter_rating.repository.CvRepo;
import com.example.waiter_rating.repository.RatingRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final RatingRepo ratingRepository;
    private final CvRepo cvRepo;

    public StatsController(RatingRepo ratingRepository, CvRepo cvRepo) {
        this.ratingRepository = ratingRepository;
        this.cvRepo = cvRepo;
    }

    @GetMapping("/professional/{professionalId}/by-month")
    public ResponseEntity<?> getRatingsByMonth(@PathVariable Long professionalId) {
        return ResponseEntity.ok(buildByMonth(professionalId));
    }

    @GetMapping("/slug/{slug}/by-month")
    public ResponseEntity<?> getRatingsByMonthSlug(@PathVariable String slug) {
        return cvRepo.findByPublicSlug(slug)
                .map(cv -> ResponseEntity.ok(buildByMonth(cv.getProfessional().getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/professional/{professionalId}/by-business")
    public ResponseEntity<?> getRatingsByBusiness(@PathVariable Long professionalId) {
        return ResponseEntity.ok(buildByBusiness(professionalId));
    }

    @GetMapping("/slug/{slug}/by-business")
    public ResponseEntity<?> getRatingsByBusinessSlug(@PathVariable String slug) {
        return cvRepo.findByPublicSlug(slug)
                .map(cv -> ResponseEntity.ok(buildByBusiness(cv.getProfessional().getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/professional/{professionalId}/by-profession-type")
    public ResponseEntity<?> getRatingsByProfessionType(@PathVariable Long professionalId) {
        return ResponseEntity.ok(buildByProfessionType(professionalId));
    }

    @GetMapping("/slug/{slug}/by-profession-type")
    public ResponseEntity<?> getRatingsByProfessionTypeSlug(@PathVariable String slug) {
        return cvRepo.findByPublicSlug(slug)
                .map(cv -> ResponseEntity.ok(buildByProfessionType(cv.getProfessional().getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== helpers ==========

    private List<Map<String, Object>> buildByMonth(Long professionalId) {
        var ratings = ratingRepository.findByProfessionalId(professionalId);

        Map<String, List<Integer>> byMonth = ratings.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().toString().substring(0, 7),
                        Collectors.mapping(r -> r.getScore(), Collectors.toList())
                ));

        return byMonth.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", entry.getKey());
                    monthData.put("average", entry.getValue().stream()
                            .mapToInt(Integer::intValue).average().orElse(0.0));
                    monthData.put("count", entry.getValue().size());
                    return monthData;
                })
                .sorted((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildByBusiness(Long professionalId) {
        List<Object[]> rows = ratingRepository.findRatingStatsByProfessionalGroupedByWorkHistory(professionalId);

        return rows.stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("workHistoryId", ((Number) row[0]).longValue());
                    item.put("business", row[1] != null ? row[1].toString() : "");
                    item.put("position", row[2] != null ? row[2].toString() : "");
                    item.put("count", ((Number) row[3]).intValue());
                    item.put("average", row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Object buildByProfessionType(Long professionalId) {
        var ratings = ratingRepository.findByProfessionalId(professionalId);

        if (ratings.isEmpty()) {
            return Collections.emptyList();
        }

        String professionType = ratings.get(0).getProfessional().getProfessionType();

        Map<String, Object> stats = new HashMap<>();
        stats.put("professionType", professionType);
        stats.put("totalRatings", ratings.size());
        stats.put("averageScore", ratings.stream()
                .mapToInt(r -> r.getScore()).average().orElse(0.0));

        return stats;
    }
}
