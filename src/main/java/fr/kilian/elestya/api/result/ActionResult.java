package fr.kilian.elestya.api.result;

public record ActionResult(
        boolean success,
        String message
) {

    public static ActionResult success(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult failure(String message) {
        return new ActionResult(false, message);

    }
}
