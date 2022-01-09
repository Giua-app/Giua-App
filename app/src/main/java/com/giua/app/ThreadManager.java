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

import java.util.List;
import java.util.Vector;

public class ThreadManager {
    private List<Thread> allThreads;
    private boolean isDestroyed = false;

    public ThreadManager() {
        allThreads = new Vector<>();
    }

    /**
     * Aggiunge alla lista un Thread e lo esegue
     *
     * @param runnable Un {@code Runnable} che dice cosa deve fare il thread
     */
    public void addAndRun(Runnable runnable) {
        allThreads.add(0, new Thread(runnable));
        allThreads.get(0).start();
    }

    /**
     * Interrompe tutti i thread startati
     */
    public void clearThreads() {
        for (Thread thread : allThreads) {
            if (thread.isAlive())
                thread.interrupt();
        }
        allThreads = new Vector<>();
    }

    /**
     * Interrompe tutti i thread startati e mette null al valore della lista.
     * ATTENZIONE: Da utilizzare SOLO quando il ThreadManager sta per essere distrutto e non verrà più utilizzato
     */
    public void destroyAllAndNullMe() {
        for (Thread thread : allThreads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        allThreads = null;
        isDestroyed = true;
    }

    public List<Thread> getAllThreads() {
        return allThreads;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}
