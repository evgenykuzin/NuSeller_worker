package evgenykuzin.core.util;

public class SearchMatcher {
    public static int check(String searchString, String foundedString) {
        int[] Di_1 = new int[foundedString.length() + 1];
        int[] Di = new int[foundedString.length() + 1];

        for (int j = 0; j <= foundedString.length(); j++) {
            Di[j] = j; // (i == 0)
        }

        for (int i = 1; i <= searchString.length(); i++) {
            System.arraycopy(Di, 0, Di_1, 0, Di_1.length);

            Di[0] = i; // (j == 0)
            for (int j = 1; j <= foundedString.length(); j++) {
                int cost = (searchString.charAt(i - 1) != foundedString.charAt(j - 1)) ? 1 : 0;
                Di[j] = min(
                        Di_1[j] + 1,
                        Di[j - 1] + 1,
                        Di_1[j - 1] + cost
                );
            }
        }
        return Di[Di.length - 1];
    }

    private static int min(int n1, int n2, int n3) {
        return Math.min(Math.min(n1, n2), n3);
    }

}
