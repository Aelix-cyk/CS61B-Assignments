# Gitlet Design Document

**Gitlet**:

## Classes and Data Structures

### Main
This is the entry of Gitlet system. Commands are handled here.

#### Fields
1. `Repository repo`



### Repository
This is the implementaion of all gitlet commands.

#### Fields


#### Methods
Every method here is corresponding to one gitlet command.
1. `init`
2. `add`
3. `commit`
4. `rm`
5. `log`
6. `global_log`
7. `find`
8. `status`
9. `checkout`
10. `branch`
11. `rm-branch`
12. `reset`
13. `merge`
 

### Blob
This class stores data of blob.

#### Fields
1. `String id`: sha1 id of the file
2. `byte[] content`: bytes of the file

#### Methods


### Commit
This class represents a commit.

#### Fields
1. `String message`: The commit message.
2. `Date timestamp`: The date and time when committing.
3. `String parent`: The string of its parent's SHA-1 id.
4. `String secondParent`: The string of its second parent's SHA-1 id.
5. `Map<String, String> trackedFiles`: The mapping of file names to blob id.

#### Methods
1. `add`: Add file to trackedFiles.
2. `remove`: Remove file from trackedFiles.
3. `hasFile`: Check if file is in trackedFiles.
4. `hasSameFile`: Check if the same file is in trackedFiles.


### Index
This class represents the staging area.

#### Fields
1. `Map<String, String> additionMap`: The mapping of file names to blob id.
2. `Set<String> removalSet`: The set of file names that would be removed from tracked files.

## Algorithms

## Persistence
The directory structure looks like this:

CWD
    - .gitlet                           <==== All persistent data is stored within here
        - commits                       <==== Where commit objects are stored
        - objects                       <==== Where blobs are stored
        - HEAD                          <==== A file that stores the name of current branch
        - STAGE                         <==== A file that stores the stage object
        - refs                          <==== References of branches
            - branches                  <==== Each file in this directory represents a branch, and stores the sha-1 id
                                              of the branch.

