// Code generated by dagger-compiler.  Do not edit.
package com.gitblit.servlet;


import dagger.internal.Binding;
import dagger.internal.Linker;
import java.util.Set;
import javax.inject.Provider;

/**
 * A {@code Binder<SparkleShareInviteServlet>} implementation which satisfies
 * Dagger's infrastructure requirements including:
 * 
 * Owning the dependency links between {@code SparkleShareInviteServlet} and its
 * dependencies.
 * 
 * Being a {@code Provider<SparkleShareInviteServlet>} and handling creation and
 * preparation of object instances.
 */
public final class SparkleShareInviteServlet$$InjectAdapter extends Binding<SparkleShareInviteServlet>
    implements Provider<SparkleShareInviteServlet> {
  private Binding<com.gitblit.manager.IRuntimeManager> runtimeManager;
  private Binding<com.gitblit.manager.IUserManager> userManager;
  private Binding<com.gitblit.manager.IAuthenticationManager> authenticationManager;
  private Binding<com.gitblit.manager.IRepositoryManager> repositoryManager;

  public SparkleShareInviteServlet$$InjectAdapter() {
    super("com.gitblit.servlet.SparkleShareInviteServlet", "members/com.gitblit.servlet.SparkleShareInviteServlet", IS_SINGLETON, SparkleShareInviteServlet.class);
  }

  /**
   * Used internally to link bindings/providers together at run time
   * according to their dependency graph.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void attach(Linker linker) {
    runtimeManager = (Binding<com.gitblit.manager.IRuntimeManager>) linker.requestBinding("com.gitblit.manager.IRuntimeManager", SparkleShareInviteServlet.class, getClass().getClassLoader());
    userManager = (Binding<com.gitblit.manager.IUserManager>) linker.requestBinding("com.gitblit.manager.IUserManager", SparkleShareInviteServlet.class, getClass().getClassLoader());
    authenticationManager = (Binding<com.gitblit.manager.IAuthenticationManager>) linker.requestBinding("com.gitblit.manager.IAuthenticationManager", SparkleShareInviteServlet.class, getClass().getClassLoader());
    repositoryManager = (Binding<com.gitblit.manager.IRepositoryManager>) linker.requestBinding("com.gitblit.manager.IRepositoryManager", SparkleShareInviteServlet.class, getClass().getClassLoader());
  }

  /**
   * Used internally obtain dependency information, such as for cyclical
   * graph detection.
   */
  @Override
  public void getDependencies(Set<Binding<?>> getBindings, Set<Binding<?>> injectMembersBindings) {
    getBindings.add(runtimeManager);
    getBindings.add(userManager);
    getBindings.add(authenticationManager);
    getBindings.add(repositoryManager);
  }

  /**
   * Returns the fully provisioned instance satisfying the contract for
   * {@code Provider<SparkleShareInviteServlet>}.
   */
  @Override
  public SparkleShareInviteServlet get() {
    SparkleShareInviteServlet result = new SparkleShareInviteServlet(runtimeManager.get(), userManager.get(), authenticationManager.get(), repositoryManager.get());
    return result;
  }
}
