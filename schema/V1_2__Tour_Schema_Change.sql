ALTER TABLE fable_tour_app.tour
    DROP COLUMN thumbnail;

ALTER TABLE fable_tour_app.tour
    ADD COLUMN description TEXT AFTER display_name;
