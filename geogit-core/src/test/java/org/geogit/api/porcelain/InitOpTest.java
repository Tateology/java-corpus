/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.porcelain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.geogit.api.CommandLocator;
import org.geogit.api.ObjectId;
import org.geogit.api.Platform;
import org.geogit.api.Ref;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.api.plumbing.UpdateRef;
import org.geogit.api.plumbing.UpdateSymRef;
import org.geogit.repository.Repository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;
import com.google.inject.Injector;

/**
 *
 */
public class InitOpTest {

    private Platform platform;

    private Injector injector;

    private InitOp init;

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File workingDir;

    private Repository mockRepo;

    private RefParse mockRefParse;

    private UpdateRef mockUpdateRef;

    private UpdateSymRef mockUpdateSymRef;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws IOException {
        CommandLocator mockCommands = mock(CommandLocator.class);

        mockRefParse = mock(RefParse.class);
        when(mockRefParse.setName(anyString())).thenReturn(mockRefParse);

        mockUpdateRef = mock(UpdateRef.class);
        when(mockUpdateRef.setName(anyString())).thenReturn(mockUpdateRef);
        when(mockUpdateRef.setDelete(anyBoolean())).thenReturn(mockUpdateRef);
        when(mockUpdateRef.setNewValue((ObjectId) anyObject())).thenReturn(mockUpdateRef);
        when(mockUpdateRef.setOldValue((ObjectId) anyObject())).thenReturn(mockUpdateRef);
        when(mockUpdateRef.setReason(anyString())).thenReturn(mockUpdateRef);

        mockUpdateSymRef = mock(UpdateSymRef.class);
        when(mockUpdateSymRef.setName(anyString())).thenReturn(mockUpdateSymRef);
        when(mockUpdateSymRef.setDelete(anyBoolean())).thenReturn(mockUpdateSymRef);
        when(mockUpdateSymRef.setNewValue(anyString())).thenReturn(mockUpdateSymRef);
        when(mockUpdateSymRef.setOldValue(anyString())).thenReturn(mockUpdateSymRef);
        when(mockUpdateSymRef.setReason(anyString())).thenReturn(mockUpdateSymRef);

        when(mockCommands.command(eq(RefParse.class))).thenReturn(mockRefParse);
        when(mockCommands.command(eq(UpdateRef.class))).thenReturn(mockUpdateRef);
        when(mockCommands.command(eq(UpdateSymRef.class))).thenReturn(mockUpdateSymRef);

        platform = mock(Platform.class);
        injector = mock(Injector.class);
        init = new InitOp(platform, injector);
        init.setCommandLocator(mockCommands);

        mockRepo = mock(Repository.class);

        workingDir = tempFolder.getRoot();

        when(platform.pwd()).thenReturn(workingDir);

    }

    @Test
    public void testNullWorkingDir() {
        when(platform.pwd()).thenReturn(null);
        exception.expect(IllegalStateException.class);
        init.call();
        when(platform.pwd()).thenReturn(workingDir);
    }

    @Test
    public void testCreateNewRepo() throws Exception {
        when(injector.getInstance(eq(Repository.class))).thenReturn(mockRepo);
        Optional<Ref> absent = Optional.absent();
        when(mockRefParse.call()).thenReturn(absent);

        Repository created = init.call();
        assertSame(mockRepo, created);
        assertTrue(new File(workingDir, ".geogit").exists());
        assertTrue(new File(workingDir, ".geogit").isDirectory());

        verify(injector, times(1)).getInstance(eq(Repository.class));
        verify(platform, atLeastOnce()).pwd();

        verify(mockUpdateRef, times(1)).setName(eq(Ref.MASTER));
        verify(mockUpdateRef, times(1)).setName(eq(Ref.WORK_HEAD));
        verify(mockUpdateRef, times(1)).setName(eq(Ref.STAGE_HEAD));
        verify(mockUpdateRef, times(3)).setNewValue(eq(ObjectId.NULL));
        verify(mockUpdateRef, times(3)).setReason(anyString());
        verify(mockUpdateRef, times(3)).call();

        verify(mockUpdateSymRef, times(1)).setName(eq(Ref.HEAD));
        verify(mockUpdateSymRef, times(1)).setNewValue(eq(Ref.MASTER));
        verify(mockUpdateSymRef, times(1)).call();
    }

    @Test
    public void testReinitializeExistingRepo() throws Exception {
        when(injector.getInstance(eq(Repository.class))).thenReturn(mockRepo);
        Optional<Ref> absent = Optional.absent();
        when(mockRefParse.call()).thenReturn(absent);

        Repository created = init.call();

        assertSame(mockRepo, created);
        verify(mockUpdateRef, times(3)).call();
        verify(mockUpdateSymRef, times(1)).call();

        assertTrue(new File(workingDir, ".geogit").exists());
        assertTrue(new File(workingDir, ".geogit").isDirectory());

        Ref master = new Ref(Ref.MASTER, ObjectId.forString("hash me"), TYPE.COMMIT);

        when(mockRefParse.call()).thenReturn(Optional.of(master));

        CommandLocator mockCommands = mock(CommandLocator.class);
        when(mockCommands.command(eq(RefParse.class))).thenReturn(mockRefParse);
        init.setCommandLocator(mockCommands);

        assertTrue(ResolveGeogitDir.lookup(platform.pwd()).isPresent());
        assertNotNull(init.call());
        verify(platform, atLeastOnce()).pwd();
        assertTrue(ResolveGeogitDir.lookup(platform.pwd()).isPresent());

        verify(mockCommands, never()).command(eq(UpdateRef.class));
        verify(mockCommands, never()).command(eq(UpdateSymRef.class));
    }

    @Test
    public void testReinitializeExistingRepoFromInsideASubdirectory() throws Exception {
        testCreateNewRepo();

        File subDir = new File(new File(workingDir, "subdir1"), "subdir2");
        assertTrue(subDir.mkdirs());

        when(platform.pwd()).thenReturn(subDir);

        assertTrue(ResolveGeogitDir.lookup(platform.pwd()).isPresent());
        assertNotNull(init.call());
        verify(platform, atLeastOnce()).pwd();
    }
}
