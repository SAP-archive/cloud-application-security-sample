package com.sap.cp.appsec.domain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AdvertisementAclRepositoryTest {
    @Autowired
    private AdvertisementAclRepository repo;
    private Advertisement entity;

    @MockBean
    private AuditorAware<String> auditorAware;

    @Before
    public void setUp() {
        when(auditorAware.getCurrentAuditor())
                .thenReturn(Optional.of("Auditor_1"))
                .thenReturn(Optional.of("Auditor_2"))
                .thenReturn(Optional.of("Auditor_3"));
        entity = new Advertisement("SOME title", "contact@email.de");
    }

    @After
    public void tearDown() {
        repo.deleteAll();
        assertThat(repo.count(), is(0L));
    }

    @Test
    public void shouldSetIdOnFirstSave() {
        entity = repo.save(entity);
        assertThat(entity.getId(), is(notNullValue()));
    }

    @Test
    public void shouldSetCreatedTimestampOnFirstSaveOnly() throws InterruptedException {
        entity = repo.save(entity);
        Timestamp timestampAfterCreation = entity.getCreatedAt();
        assertThat(timestampAfterCreation, is(notNullValue()));

        entity.setTitle("Updated Title");
        Thread.sleep(5); // Better: mock time!

        entity = repo.save(entity);
        Timestamp timestampAfterUpdate = entity.getCreatedAt();
        assertThat(timestampAfterUpdate, is(timestampAfterCreation));
    }

    @Test
    public void shouldSetCreatedByOnFirstSaveOnly() {
        entity = repo.save(entity);
        String userAfterCreation = entity.getCreatedBy();
        assertThat(userAfterCreation, is("Auditor_1"));

        entity.setTitle("Updated Title");

        entity = repo.save(entity);
        String userAfterUpdate = entity.getCreatedBy();
        assertThat(userAfterUpdate, is(userAfterCreation));
    }

    @Test
    public void shouldSetModifiedTimestampOnEveryUpdate() throws InterruptedException {
        entity = repo.save(entity);

        entity.setTitle("Updated Title");
        entity = repo.save(entity);

        Timestamp timestampAfterFirstUpdate = entity.getModifiedAt();
        assertThat(timestampAfterFirstUpdate, is(notNullValue()));

        Thread.sleep(5); // Better: mock time!

        entity.setTitle("Updated Title 2");
        entity = repo.save(entity);
        Timestamp timestampAfterSecondUpdate = entity.getModifiedAt();
        assertThat(timestampAfterSecondUpdate, is(not(timestampAfterFirstUpdate)));
    }

    @Test
    public void shouldSetModifiedByOnEveryUpdate() throws InterruptedException {
        entity = repo.save(entity);

        entity.setTitle("Updated Title");
        entity = repo.save(entity);

        String userAfterFirstUpdate = entity.getModifiedBy();
        assertThat(userAfterFirstUpdate, is("Auditor_2"));

        Thread.sleep(5); // Better: mock time!

        entity.setTitle("Updated Title 2");
        entity = repo.save(entity);
        String userAfterSecondUpdate = entity.getModifiedBy();
        assertThat(userAfterSecondUpdate, is(not(userAfterFirstUpdate)));
    }


    @Test(expected = ObjectOptimisticLockingFailureException.class)
    public void shouldUseVersionForConflicts() {
        // persists entity and sets initial version
        entity = repo.save(entity);

        entity.setTitle("entity instance 1");
        repo.save(entity); // returns instance with updated version

        repo.save(entity); // tries to persist entity with outdated version
    }

    @Test
    public void shouldFindByTitle() {
        String title = "Find me";

        entity.setTitle(title);
        repo.save(entity);

        Advertisement foundEntity = repo.findByTitle(title).get(0);
        assertThat(foundEntity.getTitle(), is(title));
    }
}