package com.vladimirkondenko.yamblz.screens.translation;

import com.vladimirkondenko.yamblz.Const;
import com.vladimirkondenko.yamblz.model.entities.Translation;
import com.vladimirkondenko.yamblz.utils.ErrorCodes;
import com.vladimirkondenko.yamblz.utils.base.BaseLifecyclePresenter;

import javax.inject.Inject;

import io.reactivex.Single;


public class TranslationPresenter extends BaseLifecyclePresenter<TranslationView, TranslationInteractor> {

    private static final String TAG = "TranslationPresenter";

    private String inputLanguage;
    private String outputLanguage;

    private Single<Translation> translationSingle = null;

    private Translation lastTranslation;

    @Inject
    public TranslationPresenter(TranslationView view, TranslationInteractor interactor) {
        super(view, interactor);
    }

    public void saveLastTranslation() {
        if (lastTranslation != null) {
            interactor.saveToHistory(lastTranslation);
        }
    }

    public void enqueueTranslation(String text) {
        if (text.length() > 0 && inputLanguage != null && outputLanguage != null) {
            if (text.length() == Const.MAX_TEXT_LENGTH) {
                view.onError(null, ErrorCodes.TEXT_TOO_LONG);
            } else {
                translationSingle = interactor.translate(inputLanguage, outputLanguage, text)
                        .compose(bindToLifecycle());
            }
        }
    }

    public void executePendingTranslation() {
        if (translationSingle != null && isViewAttached()) {
            translationSingle.subscribe(
                    translation -> {
                        view.onTranslationSuccess(translation.getResult().get(0).getValue());
                        lastTranslation = translation;
                    },
                    throwable -> view.onError(throwable, 0)
            );
        }
    }

    public void selectInputLanguage(String inputLanguage) {
        this.inputLanguage = inputLanguage;
    }

    public void selectOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
    }

}
