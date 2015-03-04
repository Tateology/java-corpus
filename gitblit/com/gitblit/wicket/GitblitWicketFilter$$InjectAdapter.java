// Code generated by dagger-compiler.  Do not edit.
package com.gitblit.wicket;


import dagger.MembersInjector;
import dagger.internal.Binding;
import dagger.internal.Linker;
import java.util.Set;
import javax.inject.Provider;

/**
 * A {@code Binder<GitblitWicketFilter>} implementation which satisfies
 * Dagger's infrastructure requirements including:
 * 
 * Owning the dependency links between {@code GitblitWicketFilter} and its
 * dependencies.
 * 
 * Being a {@code Provider<GitblitWicketFilter>} and handling creation and
 * preparation of object instances.
 * 
 * Being a {@code MembersInjector<GitblitWicketFilter>} and handling injection
 * of annotated fields.
 */
public final class GitblitWicketFilter$$InjectAdapter extends Binding<GitblitWicketFilter>
    implements Provider<GitblitWicketFilter>, MembersInjector<GitblitWicketFilter> {
  private Binding<com.gitblit.manager.IRuntimeManager> runtimeManager;
  private Binding<com.gitblit.manager.IRepositoryManager> repositoryManager;
  private Binding<com.gitblit.manager.IProjectManager> projectManager;
  private Binding<com.gitblit.dagger.DaggerWicketFilter> supertype;

  public GitblitWicketFilter$$InjectAdapter() {
    super("com.gitblit.wicket.GitblitWicketFilter", "members/com.gitblit.wicket.GitblitWicketFilter", IS_SINGLETON, GitblitWicketFilter.class);
  }

  /**
   * Used internally to link bindings/providers together at run time
   * according to their dependency graph.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void attach(Linker linker) {
    runtimeManager = (Binding<com.gitblit.manager.IRuntimeManager>) linker.requestBinding("com.gitblit.manager.IRuntimeManager", GitblitWicketFilter.class, getClass().getClassLoader());
    repositoryManager = (Binding<com.gitblit.manager.IRepositoryManager>) linker.requestBinding("com.gitblit.manager.IRepositoryManager", GitblitWicketFilter.class, getClass().getClassLoader());
    projectManager = (Binding<com.gitblit.manager.IProjectManager>) linker.requestBinding("com.gitblit.manager.IProjectManager", GitblitWicketFilter.class, getClass().getClassLoader());
    supertype = (Binding<com.gitblit.dagger.DaggerWicketFilter>) linker.requestBinding("members/com.gitblit.dagger.DaggerWicketFilter", GitblitWicketFilter.class, getClass().getClassLoader(), false, true);
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
    injectMembersBindings.add(supertype);
  }

  /**
   * Returns the fully provisioned instance satisfying the contract for
   * {@code Provider<GitblitWicketFilter>}.
   */
  @Override
  public GitblitWicketFilter get() {
    GitblitWicketFilter result = new GitblitWicketFilter(runtimeManager.get(), repositoryManager.get(), projectManager.get());
    injectMembers(result);
    return result;
  }

  /**
   * Injects any {@code @Inject} annotated fields in the given instance,
   * satisfying the contract for {@code Provider<GitblitWicketFilter>}.
   */
  @Override
  public void injectMembers(GitblitWicketFilter object) {
    supertype.injectMembers(object);
  }
}
