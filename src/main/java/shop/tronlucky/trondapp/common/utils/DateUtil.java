/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package shop.tronlucky.trondapp.common.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    /**
     * @return 获取 当前 UTC 的 %Y%M%D, 比如20190116
     */
    public static String getHumanDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    /**
     * @return 获取 当前 UTC 的 %Y%M%D, 比如20190116
     */
    public static String getHumanDate() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    /**
     * @return 获取 几天前 的 UTC 的 %Y%M%D , 比如20190116
     */
    public static String getHumanDateBefore(int day) {

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -day);
        return new SimpleDateFormat("yyyyMMdd").format(now.getTime());
    }

    /**
     * @return 获取 几天前 的 UTC 的 %Y%M%D , 比如20190116
     */
    public static String getHumanDateAfter(int day) {

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, day);
        return new SimpleDateFormat("yyyyMMdd").format(now.getTime());
    }

    public static Timestamp getTimestamp(String time) {
        try {
            return Timestamp.valueOf(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Timestamp(System.currentTimeMillis());
    }
}
