package com.catlogging.util;

import com.catlogging.model.es.EsOperatingType;
import com.catlogging.model.es.RemoteAddress;
import com.catlogging.util.value.ConfigValue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2012-2020 xzpluszone, Inc. All Rights Reserved.
 * Created by selee on 21. 12. 7..
 */

@Slf4j
public class HostInfoProp {

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\s*([^:]+):(\\d+)\\s*,?");

    public static List<RemoteAddress> parseHostInfo(ConfigValue<String> remoteAddresses){

        final List<RemoteAddress> addresses = new ArrayList<>();
        log.info("Building remote addresses from config: {}", remoteAddresses.get());
        final Matcher m = ADDRESS_PATTERN.matcher(remoteAddresses.get());
        while (m.find()) {
            final RemoteAddress ra = new RemoteAddress();
            ra.setHost(m.group(1));
            ra.setPort(Integer.parseInt(m.group(2)));
            addresses.add(ra);
        }

        return addresses;
    }
}
