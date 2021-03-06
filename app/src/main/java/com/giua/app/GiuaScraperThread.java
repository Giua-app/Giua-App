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

public class GiuaScraperThread extends Thread {
    private final Queue<Runnable> allRunnableToRun;
    private boolean isInterrupted = false;
    private boolean isRunning = false;
    private boolean isOffline = false;

    public GiuaScraperThread() {
        allRunnableToRun = new LinkedList<>();
        start();
    }

    /**
     * Disattiva il thread cancellando silenziosamente le richieste di aggiunta di task
     * ATTENZIONE: il thread rimane comunque in esecuzione in background
     * <br><br>
     *
     * @param mode {@code true} per andare offline, {@code false} per andare online
     */
    public void setOfflineMode(boolean mode) {
        isOffline = mode;
    }

    public void addTask(Runnable runnable) {
        if (!isOffline)
            allRunnableToRun.add(runnable);
    }

    public boolean isInterrupting() {
        return isInterrupted && isRunning;
    }

    /**
     * Controlla se il thread è stato interrotto, ma non tiene conto se sta comunque completando
     *
     * @return
     */
    @Override
    public boolean isInterrupted() {
        return (isInterrupted && !isRunning) || super.isInterrupted();
    }

    @Override
    public void interrupt() {
        isInterrupted = true;
        super.interrupt();
    }

    public void interruptLoop() {
        isInterrupted = true;
    }

    /**
     * Se il thread si sta interrompendo, ma non ha ancora smesso di funzionare allora
     * non farlo interrompere.
     */
    public void stopInterrupting() {
        isInterrupted = false;
    }

    public void restart() {
        interruptLoop();
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
            else {   //Se non ha niente da fare allora riposa in attesa di una task
                try {
                    sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        }
        isRunning = false;
    }
}
