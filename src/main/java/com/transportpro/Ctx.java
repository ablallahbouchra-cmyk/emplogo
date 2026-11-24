package com.transportpro;

import javafx.scene.Node;

import java.util.function.BooleanSupplier;

/** Services partagés que les pages peuvent appeler (navigation, fenêtre modale, notification). */
public interface Ctx {
    void navigate(Page page);
    void openModal(String title);
    void toast(String message);

    /** Notification d'erreur (ex. message SIGNAL d'un trigger MySQL). */
    default void toastErreur(String message) {
        toast(message);
    }

    /** Modale avec un formulaire : onSave renvoie true si l'enregistrement a réussi (la modale se ferme). */
    default void openModal(String title, Node body, BooleanSupplier onSave) {
        openModal(title);
    }
}
