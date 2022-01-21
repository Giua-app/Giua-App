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

import java.util.LinkedList;
import java.util.Queue;

public class InternetThread extends Thread {
    private final Queue<Runnable> allRunnableToRun;
    private boolean isInterrupted = false;
    private boolean isRunning = false;

    public InternetThread() {
        allRunnableToRun = new LinkedList<>();
        start();
    }

    public void addRunnableToRun(Runnable runnable) {
        allRunnableToRun.add(runnable);
    }

    @Override
    public boolean isInterrupted() {
        return isInterrupted || super.isInterrupted();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        isInterrupted = true;
    }

    public void restart() {
        interrupt();
        start();
    }

    @Override
    public void run() {
        if (isRunning) return;   //Se è già attivo non continuare
        super.run();
        isRunning = true;
        isInterrupted = false;
        while (!isInterrupted) {
            Runnable executingRunnable = allRunnableToRun.poll();
            if (executingRunnable != null)
                executingRunnable.run();
            try {
                sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        isRunning = false;
    }
}
