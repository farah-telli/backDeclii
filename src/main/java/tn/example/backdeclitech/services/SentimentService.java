package tn.example.backdeclitech.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SentimentService {

    @Value("${huggingface.api.token:#{null}}")
    private String huggingFaceToken;

    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models/nlptown/bert-base-multilingual-uncased-sentiment}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "excellent", "super", "génial", "parfait", "merveilleux", "fantastique",
            "incroyable", "magnifique", "formidable", "extraordinaire", "remarquable",
            "impressionnant", "satisfait", "content", "heureux", "agréable", "bon",
            "bien", "top", "meilleur", "qualité", "professionnel", "compétent",
            "attentionné", "aimable", "sympathique", "recommande", "adoré", "aimé"
    ));

    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
            "mauvais", "terrible", "horrible", "nul", "catastrophique", "décevant",
            "médiocre", "insuffisant", "inadmissible", "inacceptable", "lamentable",
            "pitoyable", "désagréable", "mécontent", "insatisfait", "déçu", "frustré",
            "problème", "difficile", "compliqué", "désorganisé", "incompétent",
            "impoli", "désastreux", "déconseille", "éviter", "regret"
    ));

    public enum Sentiment {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    public SentimentService() {
        this.restTemplate = new RestTemplate();
    }

    public Sentiment analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Sentiment.NEUTRAL;
        }

        if (huggingFaceToken != null && !huggingFaceToken.isEmpty()) {
            try {
                return analyzeWithHuggingFace(text);
            } catch (Exception e) {
                System.err.println("Échec de l'analyse avec HuggingFace, passage à l'analyse locale : " + e.getMessage());
            }
        }

        return analyzeLocally(text);
    }


    private Sentiment analyzeWithHuggingFace(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingFaceToken);

            Map<String, String> body = new HashMap<>();
            body.put("inputs", text);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    List.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get(0);

                String bestLabel = "";
                double maxScore = 0.0;

                for (Map<String, Object> result : results) {
                    String label = (String) result.get("label");
                    Double score = ((Number) result.get("score")).doubleValue();

                    if (score > maxScore) {
                        maxScore = score;
                        bestLabel = label;
                    }
                }

                return convertLabelToSentiment(bestLabel);
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Erreur HTTP lors de l'appel à HuggingFace : " + e.getStatusCode() + " - " + e.getMessage());
            throw new RuntimeException("Erreur HTTP " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            System.err.println("Erreur de connexion à l'API HuggingFace : " + e.getMessage());
            throw new RuntimeException("Erreur de connexion à l'API");
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de l'analyse HuggingFace : " + e.getMessage());
            throw new RuntimeException("Erreur d'analyse : " + e.getMessage());
        }

        return Sentiment.NEUTRAL;
    }


    private Sentiment convertLabelToSentiment(String label) {
        if (label.contains("5") || label.contains("4")) {
            return Sentiment.POSITIVE;
        } else if (label.contains("1") || label.contains("2")) {
            return Sentiment.NEGATIVE;
        } else {
            return Sentiment.NEUTRAL;
        }
    }

    private Sentiment analyzeLocally(String text) {
        String normalizedText = text.toLowerCase()
                .replaceAll("[^a-zàâäéèêëïîôùûüÿç\\s]", " ");

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : POSITIVE_WORDS) {
            if (Pattern.compile("\\b" + word + "\\b").matcher(normalizedText).find()) {
                positiveCount++;
            }
        }

        for (String word : NEGATIVE_WORDS) {
            if (Pattern.compile("\\b" + word + "\\b").matcher(normalizedText).find()) {
                negativeCount++;
            }
        }

        if (normalizedText.contains("pas")) {
            // Inverser les compteurs si négation détectée
            int temp = positiveCount;
            positiveCount = negativeCount;
            negativeCount = temp;
        }

        if (positiveCount > negativeCount) {
            return Sentiment.POSITIVE;
        } else if (negativeCount > positiveCount) {
            return Sentiment.NEGATIVE;
        } else {
            return Sentiment.NEUTRAL;
        }
    }


    public Map<String, Object> analyzeSentimentWithConfidence(String text) {
        Sentiment sentiment = analyzeSentiment(text);

        Map<String, Object> result = new HashMap<>();
        result.put("sentiment", sentiment);
        result.put("text", text);
        result.put("method", huggingFaceToken != null ? "HuggingFace" : "Local");

        return result;
    }
}