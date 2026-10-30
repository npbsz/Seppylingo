package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.LtItemDTO;
import com.sep.sep_backend.model.PhraseEntity;
import com.sep.sep_backend.model.Vocabulary;
import com.sep.sep_backend.repository.PhraseRepo;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LtService {
    @Autowired
    private PhraseRepo phraseRepo;
    @Autowired
    private VocabularyRepository vocabRepo;

    private final Random random = new Random();
    private static final Set<String> FILLER_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "in", "on", "at", "to", "for", "of"
    );
    private record LtResult(String textWithBlank, String solution){}

    /**
     * creates a new Lueckentext item by finding a phrase from a database
     * @return LtItemDTO to be sent to the user {id, textEN, textDE}
     */
    public LtItemDTO createNewLueckentext(){
        PhraseEntity phrase= phraseRepo.findRandomPhrase(1).getFirst();
        if (phrase == null){
            return createErrorDTO("Kein Text gefunden");
        }
        String[] words=phrase.getPhrase_EN().split("\\s+");
        if(words.length<2){
            return createErrorDTO(phrase.getPhrase_EN());
        }
        LtResult ltResult= generateLtPhrase(words);
        String[] dummyWords=generateDummyAnswers(3);
        return createCorrectDTO(phrase, ltResult,dummyWords);
    }
    //---------------Helper Methods

    /**
     * Chooses a random word from the given String[] words and sets it as the solution. Solution wont be a word from Set<String>FILLER_WORDS
     * @param words String[] with all words from fetched phrase
     * @return record Object with {String textWithBlank, String solution}
     */
    private LtResult generateLtPhrase(String[] words) {
        String removedWord;
        int removedWordIndex;

        int attempts=0;
        do {
            removedWordIndex=random.nextInt(words.length);
            removedWord=words[removedWordIndex];
            attempts++;
        }while (FILLER_WORDS.contains(removedWord.toLowerCase())&&attempts<20); //prevents an infinite loop, shouldnt be needed if DB is maintained correctly
        String solution = removedWord.replaceAll("[.,!?]", "");
        String[] workingWords = words.clone();
        workingWords[removedWordIndex] = "____";
        String textWithBlank = String.join(" ", workingWords);
        return new LtResult(textWithBlank, solution);
    }

    /**
     * Gets words from Repo and trims leading "the" or "to"
     * @param count how many dummyWords will be fetched
     * @return array with dummyWords from VocabularyRepo
     */
    private String[] generateDummyAnswers(int count) {
        String[] dummyWords = new String[count];
        List<Vocabulary> randomWords = vocabRepo.findRandomVocabs(count);
        for (int i = 0; i < count; i++) {
            String tempWord = randomWords.get(i).getWordEN();
            // expanded cleaning logic for clarity
            tempWord = tempWord.replace("the", "").replace("to", "").trim();
            dummyWords[i] = tempWord;
        }
        return dummyWords;
    }

    /**
     * Creates a valid DTO to be sent to the frontend
     * @param phrase PhraseEntity, contains the translation
     * @param ltResult record Object containing solution and a Lueckentext
     * @param dummyWords random words used for multiple choice
     * @return valid DTO
     */
    private LtItemDTO createCorrectDTO(PhraseEntity phrase, LtResult ltResult, String[] dummyWords) {
        LtItemDTO item = new LtItemDTO();
        item.setGameId(UUID.randomUUID());
        item.setAnswer(ltResult.solution());
        item.setTextWithBlank(ltResult.textWithBlank());
        item.setDummyWords(dummyWords);
        item.setTranslation(phrase.getPhrase_DE());
        return item;
    }

    /**
     * Creates an invalid DTO
     * @param text Error message
     * @return invalid DTO without unique ID
     */
    private LtItemDTO createErrorDTO(String text) {
        LtItemDTO errorItem = new LtItemDTO();
        errorItem.setTextWithBlank(text);
        errorItem.setGameId(null);
        return errorItem;
    }
}
