# TODO List

 - [ ] Support for setting default response code?
 - [x] Support for blocking assertions (ie, wait for a matching request to come in for a period of time)
 - [ ] Better (easier to read) logging, maybe not just log4j/JULI logging (specialised log)
 - [ ] Add matches/near misses to UI
 - [x] Support filtering requests via query parameters (eg, '?method=POST')
 - [ ] Create form in UI for creating stubbed responses
 - [x] Acceptance tests...
 - [ ] Tool for bulk-loading messages
 - [ ] Example Ruby/Scala/other, client code for interacting with stub (make Ruby code a Gem)
 - [ ] Persistence (async writes, persist on shutdown)
 - [ ] Explicit 'body pattern' type parameter (add support for XPath?, JSONPath?, Regular expressions, etc.)

## Github TODO

 - [ ] Add Ruby client code
 - [ ] Add some downloadable releases
 - [ ] Document JavaScript support
 - [ ] Document filtering support (/_control/requests?method=GET&path=...)
 - [ ] Binary/source release via Github
        * Increment version (to 1.0)
        * Produce assembly and upload to Github
        * Tag as release on Github
        * Ensure licence and all other licences, including where to get code are inside artefact.


