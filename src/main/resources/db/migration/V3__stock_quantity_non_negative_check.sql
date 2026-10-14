ALTER TABLE stocks
    ADD CONSTRAINT chk_stocks_quantity_non_negative CHECK (quantity >= 0);
