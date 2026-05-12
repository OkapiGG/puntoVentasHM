package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class SlugService {

    public String toSlug(String input) {
        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
