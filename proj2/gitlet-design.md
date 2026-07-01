# Gitlet Design Document

**Gitlet**:

## Classes and Data Structures

### Main
This is the entry of Gitlet system. Commands are handled here.

#### Fields

1. Field 1
2. Field 2


### Repository
This is where the main logic of our program will live.

#### Fields

1. Field 1
2. Field 2

### Commit
This class represents a commit that would be stored in a file.

#### Fields

1. `String message`: The commit message.
2. `Date timestamp`: The date and time when committing.
3. `String parent`: The string of its parent's SHA-1 id.
4. `String secondParent`: The string of its second parent's SHA-1 id.
5. `Map<String, String> trackedFiles`: The mapping of file names to blob references.

## Algorithms

## Persistence
The directory structure looks like this:

CWD
    - .gitlet                           <==== All persistent data is stored within here
        - commits                       <==== Where commit objects are stored
        - objects                       <==== Where blobs are stored
            - xx                        <==== Subdirectory which is named with the first two numbers of sha-1 id
                - xxx                   <==== binary files which are named with the rest 38 numbers of sha-1 id
        - HEAD                          <==== A file that stores the name of current branch
        - refs                          <==== References of branches
            - branches                  <==== Each file in this directory represents a branch, and stores the sha-1 id
                                              of the branch.

