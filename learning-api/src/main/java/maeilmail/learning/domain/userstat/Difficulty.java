package maeilmail.learning.domain.userstat;

public enum Difficulty {
    EASY, MEDIUM, HARD;

    public Difficulty upgrade() {
        return switch (this) {
            case EASY -> MEDIUM;
            case MEDIUM -> HARD;
            case HARD -> HARD;
        };
    }

    public Difficulty downgrade() {
        return switch (this) {
            case EASY -> EASY;
            case MEDIUM -> EASY;
            case HARD -> MEDIUM;
        };
    }
}
