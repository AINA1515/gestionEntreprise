-- Appliquer une seule fois après les scripts de création de la base.
-- Distingue la lecture in-app du statut de livraison e-mail de l'outbox.
ALTER TABLE NOTIFICATION ADD (
    EST_LUE NUMBER(1) DEFAULT 0 NOT NULL
);

ALTER TABLE NOTIFICATION ADD CONSTRAINT CK_NOTIF_EST_LUE
    CHECK (EST_LUE IN (0, 1));
