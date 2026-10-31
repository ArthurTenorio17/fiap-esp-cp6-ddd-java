package br.com.fiap.bank.plataforma.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("cpfAuthorizationService")
public class CpfAuthorizationService {

    public boolean podeAcessarCpf(String cpfSolicitado) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        boolean operadorOuAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role ->
                        role.equals("ROLE_OPERADOR")
                                || role.equals("ROLE_ADMIN")
                );

        if (operadorOuAdmin) {
            return true;
        }

        boolean cliente = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CLIENTE"::equals);

        if (!cliente) {
            return false;
        }

        String cpfToken = jwt.getClaimAsString("cpf");

        if (cpfToken == null || cpfSolicitado == null) {
            return false;
        }

        String cpfLimpoToken = cpfToken.replaceAll("\\D", "");
        String cpfLimpoSolicitado = cpfSolicitado.replaceAll("\\D", "");

        return cpfLimpoToken.length() == 11
                && cpfLimpoSolicitado.length() == 11
                && cpfLimpoToken.equals(cpfLimpoSolicitado);
    }
}
