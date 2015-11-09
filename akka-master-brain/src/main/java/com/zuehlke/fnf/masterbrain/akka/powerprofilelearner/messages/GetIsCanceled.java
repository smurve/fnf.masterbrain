package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner.messages;

/**
 * Created by mhan on 12.07.2015.
 */
public class GetIsCanceled {
    public static final GetIsCanceled INSTANCE = new GetIsCanceled();

    public GetIsCanceled() {

    }

    public static GetIsCanceled getIsCanceled() {
        return INSTANCE;
    }
}
