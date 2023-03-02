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

package com.catlogging.h2.jpa;

import com.catlogging.model.sniffer.ScannerIdataInfo;
import com.catlogging.model.sniffer.Sniffer;
import com.catlogging.model.sniffer.SnifferIdKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;


public interface SnifferScannerRepository extends JpaRepository<ScannerIdataInfo, SnifferIdKey> {


    ScannerIdataInfo save(ScannerIdataInfo s);


    @Modifying(clearAutomatically=true)
    void deleteBySnifferIdKey(SnifferIdKey key);

    Optional<ScannerIdataInfo> findScannerIdataInfoBySnifferIdKeyAndLogIn(SnifferIdKey snifferIdKey, List<String> logs);

    Optional<ScannerIdataInfo> findScannerIdataInfoBySnifferIdKeyAndLog(SnifferIdKey snifferIdKey, String log);
}