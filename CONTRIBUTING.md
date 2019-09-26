## Contributions Welcome!

We welcome contributions in many forms; there's always plenty to do!

A good way to familiarize yourself with the code and contribution process is to look for and tackle low-hanging fruit in the issue tracker. Before embarking on a more ambitious contribution, please quickly get in touch with us.

## Ways to contribute
There are multiple ways you can contribute, both as a user and as a developer. You can propose/implement features, report/fix bugs, help improve project documentation.

**Help Wanted** - The maintainers use the 'help wanted' label for issues, features and bugs, they are unable to actively work upon. Consider picking up from the list.

### Proposing new features
First, take time to review [issue tracker](https://github.com/IBM/pem-utilities/issues) to be sure that there isn't already an open (or recently closed) proposal for the same feature. If there isn't, to make a proposal we recommend that you [open an issue](https://github.com/IBM/pem-utilities/issues) and provide details of the proposal stating what the feature would do and, if possible, how it might be implemented. 

If needed, send an introductory email to the maintainers linking the issue, and soliciting feedback.

Discussion of the proposed feature should be conducted in the issue itself, so that we have a consistent pattern within our community as to where to find design discussion.

### Reporting bugs
If you have found a bug, please submit an issue [here](https://github.com/IBM/pem-utilities/issues). Before you create a new issue, please try to search the existing items to be sure no one else has previously reported it. If it has been previously reported, then you might add a comment that you also are interested in seeing the defect fixed.

When submitting a new issue, please try to provide sufficient information for someone else to reproduce the issue. One of the project's maintainers should respond to your issue within 2 business days. If not, please bump the issue with a comment and request that it be reviewed.

### Submitting your fix
If you just submitted an issue for a bug you've discovered, and would like to provide a fix, we would welcome that gladly! Please submit a pull request after submitting the issue.

A pull request doesn't have to represent finished work. It's usually better to open a pull request early on, so others can watch or give feedback on your progress. Just mark it as a "WIP” (Work in Progress) in the subject line. You can always add more commits later.

#### Basic guidelines
- [Fork the repository](https://guides.github.com/activities/forking/) and clone it locally. Connect your local to the original 'upstream' repository by adding it as a remote. Pull in changes from 'upstream' often so that you stay up to date so that when you submit your pull request, merge conflicts will be less likely. More detailed instructions can be found at GitHub's documentation.
- Create a branch for your edits.
- Include reference to the relevant issues in the pull request (for example, "Fixes #51 – <fix description>"). Also, write a meaningful commit message.
- **Build and test your changes!** Run your changes against any existing tests and create new ones when needed. Make sure your changes don’t break the existing project.
- Contribute in the style of the project to the best of your abilities. Review section 'Coding style guidelines' in this guide.

## Release Methodology
This repository does not have a traditional release management cycle, but should instead be maintained as a useful, working, and polished reference at all times. While all work can therefore be focused on the master branch, the quality of this branch should never be compromised.

## Merge approval
The project maintainers use LGTM (Looks Good To Me) in comments on the code review to indicate acceptance. A change requires LGTMs from two of the maintainers of each component affected.

For a list of the maintainers, see the [MAINTAINERS.md](https://github.com/IBM/pem-utilities/MAINTAINERS.md) page.

## Communication
Discussion of the proposed feature or bug should be conducted in the issue itself, so that we have a consistent pattern within our community as to where to find design discussion.

Direct emails to maintainers should be avoided.

## Legal
Each source file must include a license header for the Apache Software License 2.0. Using the SPDX format is the simplest approach. e.g.

```
/*
Copyright 2019 Syncsort Inc. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
```

We have tried to make it as easy as possible to make contributions. This applies to how we handle the legal aspects of contribution. We use the same approach - the [Developer's Certificate of Origin 1.1 (DCO)](https://developercertificate.org) - that the Linux® Kernel [community](https://elinux.org/Developer_Certificate_Of_Origin) uses to manage code contributions.

When submitting a patch for review, the developer must include a sign-off statement in the commit message. This sign-off certifies that the contributor accepts the DCO and has the right to contribute the code to this repository. Here is an example Signed-off-by line:

```
Signed-off-by: John Doe <john.doe@example.com>
```

If you set your user.name and user.email git configs, you can sign your commit automatically using the following command:

```
git commit -s
```

You must use your real name, not an alias or pseudonym. If you are contributing on behalf of a company or organization, you should use an email address associated with that company or organization.

## Coding style guidelines
Contribute in the style of the project to the best of your abilities. This may mean using indents, semi-colons or comments differently than you would in your own repository, but makes it easier for the maintainer to merge, others to understand and maintain in the future.

For Eclipse users, make use of the Java code formatting style available [here](https://github.com/IBM/pem-utilities/config/formatter/java_formatter.xml). If not, please review the styles and configure your IDE of choice accordingly.
