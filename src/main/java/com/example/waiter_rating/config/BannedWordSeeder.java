package com.example.waiter_rating.config;

import com.example.waiter_rating.model.BannedWord;
import com.example.waiter_rating.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BannedWordSeeder implements ApplicationRunner {

    private final BannedWordRepository bannedWordRepo;

    private static final List<String> INITIAL_WORDS = List.of(
        "pelotudo", "pelotuda", "boludo", "boluda", "forro", "forra",
        "garca", "puta", "puto", "prostituta", "mierda", "cagada",
        "culo", "concha", "pija", "verga", "chota", "hdp",
        "hijo de puta", "hija de puta", "la concha", "la puta",
        "carajo", "cag\u00f3n", "cagon", "inutil", "in\u00fatil",
        "est\u00fapido", "estupido", "est\u00fapida", "estupida",
        "idiota", "imbecil", "imb\u00e9cil", "bastardo", "bastarda",
        "mogolico", "mog\u00f3lico", "retrasado", "retrasada",
        "tarado", "tarada", "cretino", "cretina"
    );

    @Override
    public void run(ApplicationArguments args) {
        int inserted = 0;
        for (String word : INITIAL_WORDS) {
            if (!bannedWordRepo.existsByWordIgnoreCase(word)) {
                bannedWordRepo.save(BannedWord.builder().word(word).build());
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("BannedWordSeeder: {} palabras insertadas", inserted);
        }
    }
}
