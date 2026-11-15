Generate a vuejs application with vite and element plus, with the following components:
- AnimalRequestForm with
  - a dropbox having values: dog, cat
  - a number field
- ImageBrowser 
  - contains a table with following columns
    - the creation timestamp
    - the source (string)
    - a link to view the image
  - contains also a popup displaying an image
  - display http calls errors in a popup.
Data are sources in a rest API
  - creation may require some time. display fun messages "about santa preparing the parcels" in a loop.
    - Put these in a json file

Rules:
 - OptionAPI (cleaner encapsulation)
 - very simple application, the least code as possible
 - reduce comments
 - use js. no ts, but keep object programing approach.
   - no function injection
   - complexity <3 (nested function calls, nested loop ... < 3)
 - Isolate Rest calls in a class ImageServiceClient
 - use fetch api. no axios.

