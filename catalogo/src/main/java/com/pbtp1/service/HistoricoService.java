package com.pbtp1.service;

import com.pbtp1.model.Produto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HistoricoService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<?> listarRevisoesProduto(Long produtoId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.createQuery()
                .forRevisionsOfEntity(Produto.class, false, true)
                .add(AuditEntity.id().eq(produtoId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
    }

    public List<?> listarTodasRevisoes() {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.createQuery()
                .forRevisionsOfEntity(Produto.class, false, true)
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
    }

    public Produto buscarProdutoNaRevisao(Long produtoId, Number revision) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(Produto.class, produtoId, revision);
    }
}
