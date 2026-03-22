package com.example.waiter_rating.service;

import com.example.waiter_rating.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfanityFilterService {

    private final BannedWordRepository bannedWordRepo;

    @Cacheable("bannedWords")
    @Transactional(readOnly = true)
    public List<String> loadWords() {
        return bannedWordRepo.findAll().stream()
                .map(bw -> bw.getWord().toLowerCase())
                .toList();
    }

    @CacheEvict(value = "bannedWords", allEntries = true)
    public void evictCache() {}

    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        return loadWords().stream().anyMatch(lower::contains);
    }
}
