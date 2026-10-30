package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.ListExDTO;
import com.sep.sep_backend.model.PhraseEntity;
import com.sep.sep_backend.repository.PhraseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ListExService {
    @Autowired
    private PhraseRepo phraseRepo;

    /**
     * Creates new DTO by getting 4 Random phrases
     * @return ListExDTO with unique id and String[4] phrases
     */
    public ListExDTO createNewListEx(){
        ListExDTO item = new ListExDTO();
        List<PhraseEntity> randomPhrases=phraseRepo.findRandomPhrase(4);
        String[] tempPhrases = new String[randomPhrases.size()];
        for (int i = 0; i < randomPhrases.size(); i++) {
            tempPhrases[i]=randomPhrases.get(i).getPhrase_EN();
        }
        item.setPhrases(tempPhrases);
        item.setId(UUID.randomUUID());
        return item;
    }
}
