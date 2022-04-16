/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2022 Hiem, Franck1421 and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package com.giua.app;

public class AppNotificationsParams {

    /**
     * Dove vengono utilizzati:<br>
     * <ul>
     * <li><b>ID</b>: notificationManager.notify(<i><b>*_NOTIFICATION_ID</b></i>, notifica);<br></li>
     * <li><b>GOTO</b>: per sapere in che fragment deve andare l'app una volta cliccata la notifica<br></li>
     * <li><b>REQUEST_CODE</b>: PendingIntent.getActivity(context, <i><b>*_REQUEST_CODE</b></i>, intent, ...)</li>
     * </ul>
     */

    public static final int NEWSLETTERS_NOTIFICATION_ID = 10;
    public static final int NEWSLETTERS_NOTIFICATION_REQUEST_CODE = 1;
    public static final String NEWSLETTERS_NOTIFICATION_GOTO = "Newsletters";

    public static final int ALERTS_NOTIFICATION_ID = 11;
    public static final int ALERTS_NOTIFICATION_REQUEST_CODE = 2;
    public static final String ALERTS_NOTIFICATION_GOTO = "Alerts";

    public static final int VOTES_NOTIFICATION_ID = 12;
    public static final int VOTES_NOTIFICATION_REQUEST_CODE = 3;
    public static final String VOTES_NOTIFICATION_GOTO = "Votes";

    public static final int TESTS_NOTIFICATION_ID = 13;

    public static final int HOMEWORKS_NOTIFICATION_ID = 14;

    public static final int AGENDA_NOTIFICATION_REQUEST_CODE = 4;
    public static final String AGENDA_NOTIFICATION_GOTO = "Agenda";

}
