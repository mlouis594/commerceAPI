package com.mlouis594.CommerceAPI.jwt;

import com.mlouis594.CommerceAPI.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //field of the header that contains the token
        String authHeader = request.getHeader("Authorization");
        String token=null;
        String username=null;

        //extracting the token
        if(authHeader!=null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtUtil.extractUsername(token);
        }

        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails = userService.loadUserByUsername(username);

            //validate token
            if(jwtUtil.validateToken(username, userDetails, token)){

                //if token is valid set in security context
                UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                //adding details from the request to the token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //store token in context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        //calling next filter in chain
        filterChain.doFilter(request, response);
    }
}
