-- Tablodaki 'GENERATED ALWAYS AS IDENTITY' kısıtlamasını kaldırır.
-- Artık id kolonu normal bir NUMBER gibi davranacak ve Sequence'ten gelen değeri kabul edecek.
ALTER TABLE TRANSACTIONS MODIFY id DROP IDENTITY;