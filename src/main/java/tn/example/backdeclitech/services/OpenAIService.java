package tn.example.backdeclitech.services;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    private final OpenAiService client;

    public OpenAIService(@Value("${openai.api.key}") String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY non défini !");
        }
        this.client = new OpenAiService(apiKey);
    }

    public String analyzeSentiment(String text) {
        String prompt = "Analyse le sentiment du texte suivant et répond par POSITIVE, NEUTRAL ou NEGATIVE : " + text;

        CompletionRequest request = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(prompt)
                .maxTokens(10)
                .temperature(0.0)
                .build();

        return client.createCompletion(request)
                .getChoices()
                .get(0)
                .getText()
                .trim()
                .toUpperCase();
    }
}
