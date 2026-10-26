---
name: update-changelog
description: Use this skill when you're requested to update the changelog file of this project.
---

# Skill: Update Changelog

## Changelog Format

The ArkPets project uses a special format of changelog file. The changelog file of this project is `CHANGELOG.md`.

Since v3.x, each changelog section of a major/minor version should contain:

- A `##` title.
- Then one or more subsection tables.

For example:

```markdown
## v3.1
| **新增**                  |          |
|:------------------------|:---------|
| [`1234567`]             | 新增了 XXX。 |
| [`#123`]<br>[`2345678`] | 新增了 XXX。 |

| **修复**                                             |          |
|:---------------------------------------------------|:---------|
| [`#123`]<br>[`#234`]<br>[`3456789`]<br>[`4567890`] | 修复了 XXX。 |
| [`5678901`]                                        | 修复了 XXX。 |
```

### Subsections

A subsection is a **table** that shows a subset of changes that one major/minor version has made.
They must be arranged in this order:

1. The `新增` subsection is used for new features.
2. The `修复` subsection is used for bug fixes.
3. The `优化` subsection is used for improvements.
4. The `补丁` subsection is only used if there are patch versions of this major/minor version.

In the table, the row order depends on the change date (earlier comes first).

### References Column

The first column of a subsection table is called *the references column*. For example:

- `` [`1234567`] ``: This describes the commit that introduced the change.
- `` [`1234567`]<br>[`2345678`] ``: You can attach more commits and use `<br>` between them.
- `` [`#123`]<br>[`1234567`] ``: You can also attach one or more related issues and PRs.
- `` `v3.1.2`<br>[`#123`]<br>[`1234567`] ``: If the change is in a patch version, you must point out the patch version.

### Description Column

The second column of a subsection table is called *the description column*.
In this column, you should use past tense to write sentences.

### Links Store

At the end of the file, we have a special part that stores markdown links. For example:

```markdown
<!-- Links to v3.x References -->
[`#123`]: https://github.com/isHarryh/Ark-Pets/issues/123
[`#456`]: https://github.com/isHarryh/Ark-Pets/pull/456
[`1234567`]: https://github.com/isHarryh/Ark-Pets/commit/1234567......
[`2345678`]: https://github.com/isHarryh/Ark-Pets/commit/2345678......
```

Sorting rules:
- Issues and PRs should be placed before commit links.
- An issue/PR should be placed before another issue/PR only if its number is smaller.
- A commit link should be placed before another commit link only if its authoring date is earlier.

However, if a markdown link is a third-party link but not issue/PR/commit link,
it should not use the links store but just use normal markdown link grammar like `[Link](http://...)`.

## Common Workflows

If the user asks you to update the changelog, you should confirm what the target version is,
and what the type of the version (major/minor or patch) is.

Then you should read the git history to compare the changes from the last existing version to the target version.
