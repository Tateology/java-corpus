// Code generated by dagger-compiler.  Do not edit.
package com.gitblit.servlet;


import dagger.internal.Binding;
import dagger.internal.Linker;
import java.util.Set;
import javax.inject.Provider;

/**
 * A {@code Binder<DownloadZipServlet>} implementation which satisfies
 * Dagger's infrastructure requirements including:
 * 
 * Owning the dependency links between {@code DownloadZipServlet} and its
 * dependencies.
 * 
 * Being a {@code Provider<DownloadZipServlet>} and handling creation and
 * preparation of object instances.
 */
public final class DownloadZipServlet$$InjectAdapter extends Binding<DownloadZipServlet>
    implements Provider<DownloadZipServlet> {
  private Binding<com.gitblit.manager.IRuntimeManager> runtimeManager;
  private Binding<com.gitblit.manager.IRepositoryManager> repositoryManager;

  public DownloadZipServlet$$InjectAdapter() {
    super("com.gitblit.servlet.DownloadZipServlet", "members/com.gitblit.servlet.DownloadZipServlet", IS_SINGLETON, DownloadZipServlet.class);
  }

  /**
   * Used internally to link bindings/providers together at run time
   * according to their dependency graph.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void attach(Linker linker) {
    runtimeManager = (Binding<com.gitblit.manager.IRuntimeManager>) linker.requestBinding("com.gitblit.manager.IRuntimeManager", DownloadZipServlet.class, getClass().getClassLoader());
    repositoryManager = (Binding<com.gitblit.manager.IRepositoryManager>) linker.requestBinding("com.gitblit.manager.IRepositoryManager", DownloadZipServlet.class, getClass().getClassLoader());
  }

  /**
   * Used internally obtain dependency information, such as for cyclical
   * graph detection.
   */
  @Override
  public void getDependencies(Set<Binding<?>> getBindings, Set<Binding<?>> injectMembersBindings) {
    getBindings.add(runtimeManager);
    getBindings.add(repositoryManager);
  }

  /**
   * Returns the fully provisioned instance satisfying the contract for
   * {@code Provider<DownloadZipServlet>}.
   */
  @Override
  public DownloadZipServlet get() {
    DownloadZipServlet result = new DownloadZipServlet(runtimeManager.get(), repositoryManager.get());
    return result;
  }
}
