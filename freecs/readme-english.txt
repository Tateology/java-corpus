This is the beta releas of FreeCS verion 1.2 (the free chat server)

Within the /lib-folder there are the jar-files needed to run this 
server. If you want to use the SQL-authentication, you must supply
a jdbc-driver within the /lib/ext-folder and add it to the classpath.

Within the /config-folder you can find files, which configure the
behavior of this chat-server. Documentation may be found inside the
config-files.

The /templatesets-folder contains some example-templates in rough
style. Each templateset has files for each site delivered by this
chatserver plus a message.set-file. This message.set-file contains
all server-generated messages and the layout for each message may be
deffined per templateset. To access a specific templateset, you have
to give an argument within your request (either post or get):
templateset=[templatesetname==foldername]
(e.g.: http://yourserver.name:1976/?templateset=dark)

For help you may find answeres or post questions to the message-boards
fond at http://freecs.sourceforge.net. (Tough it is not guaranteed to
get support imediately. It mostly depends on available time...)

The /static-folder contains static images (jpeg, jpg, gif) to be served
by the chatserver. colorbar.gif for example, is referenced inside the
example-template input.html.

Some other documentation may be found below the docs-folder.