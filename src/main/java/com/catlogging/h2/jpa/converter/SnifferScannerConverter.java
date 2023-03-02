/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.catlogging.h2.jpa.converter;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.ConfigException;
import com.catlogging.event.Scanner;
import com.catlogging.event.filter.FilteredScanner;
import com.catlogging.model.sniffer.Sniffer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
@Component
@Slf4j
public class SnifferScannerConverter implements AttributeConverter<FilteredScanner, String> {  //Convert할 Entity 요소와 DB타입을 표시

    @Autowired
    private BeanConfigFactoryManager configManager;

    //    Entity → 데이터베이스로 변환할 메소드, 저장용도
    @Override
    public String convertToDatabaseColumn(FilteredScanner scanner) {
        return configManager.saveBeanToJSON(Scanner.ScannerWrapper.unwrap(scanner));
    }

    //    데이터베이스 → Entity로 변환할 메소드, 읽기용도
    @Override
    public FilteredScanner convertToEntityAttribute(String dbData) {

        if (StringUtils.isEmpty(dbData)) {
            return null;
        }

        return new FilteredScanner.FilteredScannerWrapper() {
            @Override
            public FilteredScanner getWrapped() throws ConfigException {
                try {
                    return configManager.createBeanFromJSON(FilteredScanner.class, dbData);
                } catch (final ConfigException e) {
                    log.warn("Failed to deserilize scanner as filtered scanner, try to map as common scanner: " + dbData, e);
                    return new FilteredScanner(configManager.createBeanFromJSON(Scanner.class, dbData));
                }
            }
        };
    }
}