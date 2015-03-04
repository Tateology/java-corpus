// Code generated by dagger-compiler.  Do not edit.
package com.gitblit.servlet;


import dagger.internal.Binding;
import dagger.internal.Linker;
import java.util.Set;
import javax.inject.Provider;

/**
 * A {@code Binder<SyndicationServlet>} implementation which satisfies
 * Dagger's infrastructure requirements including:
 * 
 * Owning the dependency links between {@code SyndicationServlet} and its
 * dependencies.
 * 
 * Being a {@code Provider<SyndicationServlet>} and handling creation and
 * preparation of object instances.
 */
public final class SyndicationServlet$$InjectAdapter extends Binding<SyndicationServlet>
    implements Provider<SyndicationServlet> {
  private Binding<com.gitblit.manager.IRuntimeManager> runtimeManager;
  private Binding<com.gitblit.manager.IRepositoryManager> repositoryManager;
  private Binding<com.gitblit.manager.IProjectManager> projectManager;

  public SyndicationServlet$$InjectAdapter() {
    super("com.gitblit.servlet.SyndicationServlet", "members/com.gitblit.servlet.SyndicationServlet", IS_SINGLETON, SyndicationServlet.class);
  }

  /**
   * Used internally to link bindings/providers together at run time
   * according to their dependency graph.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void attach(Linker linker) {
    runtimeManager = (Binding<com.gitblit.manager.IRuntimeManager>) linker.requestBinding("com.gitblit.manager.IRuntimeManager", SyndicationServlet.class, getClass().getClassLoader());
    repositoryManager = (Binding<com.gitblit.manager.IRepositoryManager>) linker.requestBinding("com.gitblit.manager.IRepositoryManager", SyndicationServlet.class, getClass().getClassLoader());
    projectManager = (Binding<com.gitblit.manager.IProjectManager>) linker.requestBinding("com.gitblit.manager.IProjectManager", SyndicationServlet.class, getClass().getClassLoader());
  }

  /**
   * Used internally obtain dependency information, such as for cyclical
   * graph detection.
   */
  @Override
  public void getDependencies(Set<Binding<?>> getBindings, Set<Binding<?>> injectMembersBindings) {
    getBindings.add(runtimeManager);
    getBindings.add(repositoryManager);
    getBindings.add(projectManager);
  }

  /**
   * Returns the fully provisioned instance satisfying the contract for
   * {@code Provider<SyndicationServlet>}.
   */
  @Override
  public SyndicationServlet get() {
    SyndicationServlet result = new SyndicationServlet(runtimeManager.get(), repositoryManager.get(), projectManager.get());
    return result;
  }
}
