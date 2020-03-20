package com.sdm.core.service;

import com.sdm.Constants;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.model.ClientInfo;
import com.sdm.core.repository.ClientRepository;
import com.sdm.core.util.Globalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
public class ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private ClientRepository repository;

    @Autowired
    private SecurityProperties securityProperties;

    private void writeLog(String type, String remoteAddress, String url) {
        Marker marker = MarkerFactory.getMarker(type.toUpperCase());
        logger.info(marker, "{} => {}", remoteAddress, url);
    }

    private String readBody(BufferedReader buffIn) throws IOException {
        StringBuilder everything = new StringBuilder();
        String line;
        while ((line = buffIn.readLine()) != null) {
            everything.append(line);
        }
        return everything.toString();
    }

    @Transactional
    public boolean isBlocked(HttpServletRequest request) {
        String type = request.getMethod();
        String url = request.getRequestURI();
        String remoteAddress = Globalizer.getRemoteAddress(request);
        writeLog(type, remoteAddress, url);

        Date now = new Date();

        ClientInfo client = repository.findOneByRemoteAddress(remoteAddress)
                .orElse(new ClientInfo(UUID.randomUUID().toString(), remoteAddress, null, null));
        client.setLastRequestAt(now);
        boolean blocked = client.getBlockedExpiry() != null && client.getBlockedExpiry().after(now);


        HttpSession session = request.getSession(false);

        //Check Auth failed Count
        int authCount = 0;
        if (session != null && session.getAttribute(Constants.SESSION.AUTH_FAILED_COUNT) != null) {
            authCount = (int) session.getAttribute(Constants.SESSION.AUTH_FAILED_COUNT);
        }

        //Check JWT failed count
        int jwtCount = 0;
        if (session != null && session.getAttribute(Constants.SESSION.JWT_FAILED_COUNT) != null) {
            jwtCount = (int) session.getAttribute(Constants.SESSION.JWT_FAILED_COUNT);
        }

        //Blocked Client if Failed count reached limit.
        if (authCount >= securityProperties.getAuthFailedCount() || jwtCount >= securityProperties.getAuthFailedCount()) {
            Date blockedDate = Globalizer.addDate(new Date(), securityProperties.getBlockedTime());
            client.setBlockedExpiry(blockedDate);
            blocked = true;
        }

        //Unblock Client
        if (!blocked && authCount < securityProperties.getAuthFailedCount() && jwtCount < securityProperties.getAuthFailedCount()) {
            client.setBlockedExpiry(null);
        }

        repository.save(client);

        return blocked;
    }

}
