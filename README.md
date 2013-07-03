Flowdock Confluence Plugin
==========================

Installation
------------

Install [Atlassian Plugin SDK](https://developer.atlassian.com/display/HOME/Welcome)

Execute `atlas-run` to run Confluence locally. That will install all the
dependencies using maven. Default login credentials are `admin/admin`

If it seems that confluence is running wrong version, try clean the environment
and force the version by using --version parameter, e.g:

    atlas-clean
    atlas-run --version 5.2-m19

Configuration
-------------

Configurate Flow plugin like explained in Flowdock's
[help](https://flowdock.com/help/confluence) section.

When running Confluence with `atlas-run`, Flowdock plugin can be found from
System plugins.
