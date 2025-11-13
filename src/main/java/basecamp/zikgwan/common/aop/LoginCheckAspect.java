package basecamp.zikgwan.common.aop;

import basecamp.zikgwan.common.exception.UnauthorizedException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoginCheckAspect {

    @Before("@annotation(basecamp.zikgwan.common.aop.LoginCheck)")
    public void checkLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("Authorization header is missing or token is invalid");
        }
    }

}
