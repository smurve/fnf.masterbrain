package com.zuehlke.fnf.masterbrain.akka.messages;

public class ResetCommand {

    private static final ResetCommand MESSAGE = new ResetCommand();

    private ResetCommand() {

    }

    public static ResetCommand message() {
        return MESSAGE;
    }
}
