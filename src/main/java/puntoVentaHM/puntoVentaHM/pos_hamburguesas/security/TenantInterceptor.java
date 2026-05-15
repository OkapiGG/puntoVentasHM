package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;

/**
 * Verifica que cualquier endpoint que reciba {idNegocio} en el path corresponda al
 * negocio del usuario autenticado. Ignora endpoints que no exijan negocio o sin token.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final AccessControlService accessControlService;

    public TenantInterceptor(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        @SuppressWarnings("unchecked")
        Map<String, String> uriVariables = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
        );
        if (uriVariables == null) {
            return true;
        }
        String raw = uriVariables.get("idNegocio");
        if (raw == null || raw.isBlank()) {
            return true;
        }

        Long idNegocio;
        try {
            idNegocio = Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            return true; // dejar que el controller falle naturalmente
        }

        String token = accessControlService.extraerToken(request);
        if (token == null) {
            return true; // controller decidirá si requiere auth
        }

        Usuario usuario = accessControlService.requireAuthenticated(request);
        accessControlService.requireSameTenant(usuario, idNegocio);
        return true;
    }
}
