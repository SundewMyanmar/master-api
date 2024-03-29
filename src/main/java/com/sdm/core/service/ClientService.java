package com.sdm.core.service;

import com.sdm.core.Constants;
import com.sdm.core.db.repository.ClientRepository;
import com.sdm.core.model.ClientInfo;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ClientService {

    @Autowired
    private ClientRepository repository;

    @Autowired
    private SecurityManager securityManager;

    private void writeLog(String type, String remoteAddress, String url) {
        Marker marker = MarkerManager.getMarker(type.toUpperCase());
        log.info(marker, "{} => {}", remoteAddress, url);
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

        ClientInfo client = repository.findFirstByRemoteAddress(remoteAddress)
                .orElse(new ClientInfo(UUID.randomUUID().toString(), remoteAddress));
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
        if (authCount >= securityManager.getProperties().getAuthFailedCount() || jwtCount >= securityManager.getProperties().getAuthFailedCount()) {
            Date blockedDate = Globalizer.addDate(new Date(), Duration.ofMinutes(securityManager.getProperties().getAuthFailedMinuteOfBlock()));
            client.setBlockedExpiry(blockedDate);
            blocked = true;
        }

        //Unblock Client
        if (!blocked && authCount < securityManager.getProperties().getAuthFailedCount() && jwtCount < securityManager.getProperties().getAuthFailedCount()) {
            client.setBlockedExpiry(null);
        }

        repository.save(client);

        return blocked;
    }

}
