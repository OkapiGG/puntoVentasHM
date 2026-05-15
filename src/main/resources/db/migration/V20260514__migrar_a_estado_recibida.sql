-- Migración: introducir estado POR_ACEPTAR para órdenes que aún no han sido aceptadas por cocina.
-- Estrategia:
--   1. Toda orden en estado PREPARANDO que NO tenga pago registrado se asume "todavía no aceptada
--      por cocina" y se mueve a POR_ACEPTAR. Esto preserva la capacidad de cancelarlas.
--   2. Lo mismo aplica al estado_entrega del domicilio asociado.
--   3. Las órdenes que ya tienen pago registrado conservan su estado actual (PREPARANDO/LISTO/etc.)
--      porque ya pasaron por cocina o ya quedaron cerradas con el flujo anterior.
--   4. Si una iteración previa de esta migración dejó valores RECIBIDA, los normaliza a POR_ACEPTAR.
--
-- Ejecutar una sola vez, dentro de una transacción. Idempotente: si no hay candidatos, no afecta.

BEGIN;

UPDATE ordenes
SET estado = 'POR_ACEPTAR'
WHERE estado = 'RECIBIDA';

UPDATE orden_domicilio
SET estado_entrega = 'POR_ACEPTAR'
WHERE estado_entrega = 'RECIBIDA';

UPDATE ordenes
SET estado = 'POR_ACEPTAR'
WHERE estado = 'PREPARANDO'
  AND id_orden NOT IN (SELECT DISTINCT id_orden FROM pagos);

UPDATE orden_domicilio od
SET estado_entrega = 'POR_ACEPTAR'
WHERE od.estado_entrega = 'PREPARANDO'
  AND od.id_orden IN (
      SELECT o.id_orden
      FROM ordenes o
      WHERE o.estado = 'POR_ACEPTAR'
  );

COMMIT;
