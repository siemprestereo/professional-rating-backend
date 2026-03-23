ALTER TABLE ratings DROP FOREIGN KEY FKfdb6sla5l4ode4qrco55g1hu9;
ALTER TABLE ratings ADD CONSTRAINT fk_ratings_work_history
    FOREIGN KEY (work_history_id) REFERENCES work_history(id) ON DELETE SET NULL;
