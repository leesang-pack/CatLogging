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
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Publisher;
import com.catlogging.event.Scanner;
import com.catlogging.util.LazyList;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
@Component
@Slf4j
public class SnifferPublisherConverter implements AttributeConverter<List<Publisher>, String> {  //Convert할 Entity 요소와 DB타입을 표시

    @Autowired
    private BeanConfigFactoryManager configManager;

    @Autowired
    private ObjectMapper objectMapper;

    //    Entity → 데이터베이스로 변환할 메소드, 저장용도
    @Override
    public String convertToDatabaseColumn(List<Publisher> object) {
        final StringBuilder jsonArray = new StringBuilder("[");
        if(!CollectionUtils.isEmpty(object)) {
            for (final Publisher pub : object) {
                if (jsonArray.length() > 1) {
                    jsonArray.append(",");
                }
                jsonArray.append(configManager.saveBeanToJSON(Publisher.PublisherWrapper.unwrap(pub)));
            }
        }
        jsonArray.append("]");
        return jsonArray.toString();
    }

    //    데이터베이스 → Entity로 변환할 메소드, 읽기용도
    @Override
    public List<Publisher> convertToEntityAttribute(String dbData) {

        return new LazyList<Publisher>(new LazyList.ListFactory<Publisher>() {
            @Override
            public List<Publisher> createList() {
                final List<Publisher> pubs = new ArrayList<Publisher>();
                try {
                    final Publisher[] deserialized = objectMapper.readValue(dbData, Publisher[].class);
                    for (final Publisher p : deserialized) {
                        pubs.add(p);
                    }
                } catch (final IOException e) {
                    throw new ConfigException("Failed to deserialize publishers", e);
                }
                return pubs;
            }
        });
    }
}