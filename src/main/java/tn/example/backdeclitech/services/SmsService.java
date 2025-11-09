package tn.example.backdeclitech.services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SmsService {

    private final String FROM_PHONE = "+15078531483";

    private final Set<String> verifiedNumbers = Set.of(
            "+21623099545"
    );

    @PostConstruct
    public void initTwilio() {
        System.out.println("✅ Twilio initialisé");
    }

    public void sendSms(String to, String message) {
        try {
            String cleanedPhone = to.replaceAll("\\s+", "").trim();
            if (!cleanedPhone.startsWith("+216")) {
                cleanedPhone = "+216" + cleanedPhone;
            }

            System.out.println("📞 Numéro formaté : " + cleanedPhone);
            System.out.println("📨 Message à envoyer : " + message);

            if (!verifiedNumbers.contains(cleanedPhone)) {
                System.out.println("🔁 [MODE TEST - SMS NON ENVOYÉ]");
                System.out.println("🧾 Simulation SMS à " + cleanedPhone + " : " + message);
                return;
            }

            Message msg = Message.creator(
                    new PhoneNumber(cleanedPhone),
                    new PhoneNumber(FROM_PHONE),
                    message
            ).create();

            System.out.println("✅ SMS envoyé avec succès. SID : " + msg.getSid());
            System.out.println("📊 Statut de l'envoi : " + msg.getStatus());

        } catch (ApiException e) {
            System.err.println("❌ Erreur Twilio : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur générale : " + e.getMessage());
        }
    }

}
