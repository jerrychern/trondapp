package shop.tronlucky.trondapp.controller.http;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TestDaoJiShi {
    public static long totalTime = 60 * 1L;

    public static void main(String[] args) {
        int[] A = {1, 2, 5, 4, 4, 1, 2};
        int n = A[0];
        for (int i = 1; i < A.length; i++) {
            n = n ^ A[i];
        }
        System.out.println(n);
    }

    private static void time3() {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                totalTime--;
                long mm = totalTime / 60 % 60;
                long ss = totalTime % 60;
                System.out.println("还剩" + mm + "分钟" + ss + "秒");
                if (totalTime == 0) {
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }
}
