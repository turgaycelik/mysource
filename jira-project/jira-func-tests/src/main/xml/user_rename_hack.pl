#!/usr/bin/perl

# Given a list of XML files (or directories within which to do a recursive search
# for them), this script hacks renamed and recycled users into it.  It does this
# by watching for items in the XML that are supposed to be user keys instead of
# usernames now and changing them to some new value for each user.
#
# It alternates between names like ID10100 (to resemble a recycled user) and whatever
# the previous user was that it mapped as such.  In other words, if it comes across
# Alice, Bob, Carol, Dave, and Eve in that order, then it might assign them as:
#    USERKEY    USERNAME    REASONING
#    ID10100    Alice       Alice is recycled (Bob is using her key)
#    alice      Bob         Bob used to be Alice
#    ID10200    Carol       Carol is recycled (Dave is using her key)
#    carol      Dave        Dave used to be Carol
#    ID10201    Eve         Eve is recycled (but the user count is odd)
#
# Since there are only 5 users, Eve's user key doesn't end up assigned to another
# user, but nothing in the code should be bothered by this.  This could happen,
# for example, if Eve was originally created as Carol just like Dave was.  The
# confusion about the username "Alice" and the userkey "alice" is what we're
# after for testing purposes.
#
# In the ideal world, functional tests generally shouldn't care what the user's
# key is, because the end user shouldn't need to know.  In practice, this is
# almost certainly not true, as the tests may in some cases see the key in the
# raw HTML where it gets used as a column ID or something similar.  Those tests
# could be modified to take the user key from the live system instead of assuming
# the value up front or to take the user's username or display name from the data
# instead.  We do not plan to ever reveal the user's key in the UI, so some other
# identifying information should always be present.
#
# That said, as long as we can tell that the problem is coming from how the test
# was written as opposed to the production code, that's good enough for me for
# now.

use strict;

# User custom field types.  We don't actually pay any attention to which kind
# it is, as we don't generally need to care.
use constant SINGLE => 1;
use constant MULTI => 2;

# Autoflush on for stdout/stderr
select(STDERR); $|++;
select(STDOUT); $|++;

# Globals
my $VERBOSE = 0;
my $currentFile = undef;
my $previousUsername = undef;
my $key2name = {};
my $name2key = {};
my $counter = 50000;
my $customFieldIds = {};
my $customFieldNames = {};
my $line = undef;
my $saved = undef;

sub generateKey($$) {
	my ($id, $lowerUserName) = @_;
	if (defined $previousUsername) {
		my $ret = $previousUsername;
		$previousUsername = undef;
		return $ret;
	}
	$previousUsername = $lowerUserName;
	return 'ID' . $id;
}

sub findXmlsAndDirs($) {
	my ($dir) = @_;
	if (!opendir(DIR,$dir)) {
		warn "opendir: $dir: $!\n";
		return ([], []);
	}

	my $xmls = [];
	my $dirs = [];
	while (my $file = readdir(DIR)) {
		next if $file =~ m#^\.#;
		my $path = $dir . '/' . $file;
		push @$xmls, $path if $file =~ m#\.xml$#;
		push @$dirs, $path if -d $path;
	}

	my @ret = grep(/\.xml$/, readdir(DIR));
	closedir DIR;
	return ($xmls, $dirs);
}

sub findAllTargets(@) {
	my @ToDo = @_;
	my $targets = [];
	while (@ToDo) {
		my $target = shift @ToDo;
		if (-f $target) {
			push @$targets, $target;
			next;
		}
		die "Target does not exist: $target\n" unless -d $target;
		my ($xmls, $dirs) = findXmlsAndDirs($target);
		push @$targets, @$xmls;
		push @ToDo, @$dirs;
	}
	return $targets;
}

sub mapKey($) {
	my ($name) = @_;
	return '' unless length $name;
	my $key = $name2key->{$name};
	return $key if defined $key;

	die "$currentFile: Collision on lowerUserName $name!  $line" if exists $key2name->{$name};
	warn "$currentFile: Looks like you grabbed an ID column by mistake...  name=[$name]: $line" if $name =~ /^10\d\d\d$/;
	warn "$currentFile: Missing key mapping for '$name'; mapping it to itself.  $line";
	$name2key->{$name} = $name;
	$key2name->{$name} = $name;
	return $name;
}

sub mapKeys($) {
	my ($value) = @_;
	return mapKey($value) unless $value =~ /^\[(.*?)\]$/;
	my @keys = split(/, /, $1);
	return '[' . join(', ', map { mapKey($_) } @keys) . ']';
}

sub clearCache() {
	$key2name = {};
	$name2key = {};
	$previousUsername = undef;
	$counter = 50000;
	$customFieldIds = {};
	$customFieldNames = {};
}

sub cancel(@) {
	warn $currentFile, ': ', @_, "\n";
	close IN;
	close OUT;
	unlink '.tmpfile.xml';  # Ignore errors... maybe we just didn't get that far
	return 0;
}

#
# During this first pass through the file we're really after three things:
# 1) Make sure the file is okay to work with.  The requirements are:
#	A) It can't have "suppresschecks: upgrade" anywhere in it.  That
#		generally means it's an upgrade task test or otherwise shouldn't
#		be messed with.
#	B) It has to already have ApplicationUser entities; otherwise it is
#		in an older format and we can't be sure we know what to do with it.
#       We will also flag any existing OSUser entities as a problem, because
#       they should be gone.
#	C) All of the ApplicationUser entities must have the same value for
#		userKey and lowerUserName.  Otherwise, someone has already renamed
#		a user.  Presumably this means that the test is already aware of
#       renamed users and specifically covers them as part of the testing,
#       which we would very likely mess up by moving things around further.
# 2) Build up the mapping of what keys will get assigned to which users
# 3) Note which custom fields use the User Picker or Multi User Picker
#    type and therefore need to be be treated as user references when we
#    process their change items and current values.
#
sub prescanFile() {
	clearCache();
	if (!open(IN, $currentFile)) {
		warn "open: $currentFile: $!\n";
		return 0;
	}

	print "$currentFile: Scanning ...\n" if $VERBOSE >= 2;
	while (my $line = <IN>) {
		return cancel('Suppresses upgrades') if $line =~ /suppresscheck.*upgrade/;

		if ($line =~ /(<ApplicationUser.*id=")(.*?)(".*userKey=")(.*?)(".*lowerUserName=")(.*?)(".*)/) {
			my ($id, $userKey, $lowerUserName) = ($2,$4,$6);
			return cancel('Already has renamed or recycled users in it') if $userKey ne $lowerUserName;
			return cancel('Has duplicate user key in it?!') if exists $name2key->{$lowerUserName};
			$userKey = generateKey($id, $lowerUserName);
			print "$currentFile:   Mapping key '$userKey' for user '$lowerUserName'\n" if $VERBOSE >= 2;
			return cancel('Collision on userKey ', $userKey, '?!') if exists $key2name->{$userKey};
			$key2name->{$userKey} = $lowerUserName;
			$name2key->{$lowerUserName} = $userKey;
			next;
		}

		if ($line =~ /<CustomField.*id="(.*?)".*customfieldtypekey="(.*?)".*name="(.*?)"/) {
			my ($id, $type, $name) = ($1, $2, $3);
			if ($type =~ /^com.atlassian.jira.plugin.system.customfieldtypes:(multi)?userpicker$/) {
				return cancel('Duplication custom field id ', $id) if exists $customFieldIds->{$id};
				return cancel('Duplication custom field name ', $name) if exists $customFieldNames->{$name};
				$customFieldIds->{$id} = $name;
				$customFieldNames->{$name} = $1 ? MULTI : SINGLE;
			}
			next;
		}

		if ($line =~ /<OSUser/) {
			return cancel('Has one or more OSUser entities');
		}
	}

	return cancel('No ApplicationUser entities found') unless scalar %$key2name;
	if ($VERBOSE >= 3 && scalar %$customFieldIds) {
		print "$currentFile:  Will map custom fields:\n";
		foreach my $id (sort keys %$customFieldIds) {
			my $name = $customFieldIds->{$id};
			my $type = $customFieldNames->{$name};
			print "$currentFile:    $id: $name ($type)\n";
		}
	}
	return 1;
}

sub debugMapping($$$$) {
	print "$currentFile:      $_[0].$_[1]: $_[2] -> $_[3]\n";
}

sub processFile($) {
	$currentFile = $_[0];
	return unless prescanFile();

	if (!open(IN, $currentFile)) {
		warn "open: $currentFile: $!\n";
		return;
	}
	open OUT, '>.tmpfile.xml' or die "open: .tmpfile.xml: $!\n";

	# Scan for user references and update them to the substituted keys when found
	print "$currentFile: Writing ...\n" if $VERBOSE >= 2;

	$saved = undef;
	while ($line = <IN>) {
		# Entity might span multiple lines, so keep gathering content until
		# we have the whole thing.  So simplify things, this has the side-effect
		# of writing it out as a single line.
		if (defined $saved) {
			$line = $saved . $line;
			$saved = undef;
		}
		if ($line =~ /^\s*<[^>]*$/) {
			$line =~ y/\r\n/ /s;
			$saved = $line;
			next;
		} 

		# The author and updateauthor of a comment
		if ($line =~ /<Action /) {
			if ($line =~ /^(.* author=")(.*?)(".*)/) {
				debugMapping('Action', 'author', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			if ($line =~ /^(.* updateauthor=")(.*?)(".*)/) {
				debugMapping('Action', 'updateauthor', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			print OUT $line;
			next;
		}

		# The actual userkey-username mapping
		if ($line =~ /^(\s*<ApplicationUser .*userKey=")(.*?)(".*)/) {
			debugMapping('ApplicationUser', 'userKey', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# The owner of a user-supplied avatar
		if ($line =~ /^(\s*<Avatar .*avatarType="user".*owner=")(.*?)(".*)/) {
			debugMapping('Avatar[type=user]', 'owner', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# The user who edited an issue
		if ($line =~ /^(\s*<ChangeGroup .*author=")(.*?)(".*)/) {
			debugMapping('ChangeGroup', 'author', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Changes to assignee, reporter, and user custom fields
		if ($line =~ /<ChangeItem .*fieldtype="(.*?)".*field="(.*?)"/) {
			my ($type, $field) = ($1, $2);
			if (($type eq 'jira' && ($field eq 'reporter' || $field eq 'assignee')) ||
					($type eq 'custom' && exists($customFieldNames->{$field})) )
			{
				if ($line =~ /^(.*oldvalue=")(.*?)(".*)/) {
					debugMapping('ChangeItem', 'oldvalue', $2, mapKeys($2)) if $VERBOSE >= 4;
					$line = $1 . mapKeys($2) . $3 . "\n";
				}
				if ($line =~ /^(.*newvalue=")(.*?)(".*)/) {
					debugMapping('ChangeItem', 'newvalue', $2, mapKeys($2)) if $VERBOSE >= 4;
					$line = $1 . mapKeys($2) . $3 . "\n";
				}
			}
			print OUT $line;
			next;
		}

		# The user's preferred column layout
		if ($line =~ /^(\s*<ColumnLayout .*username=")(.*?)(".*)/) {
			debugMapping('ColumnLayout', 'username', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Component lead
		if ($line =~ /^(\s*<Component .*lead=")(.*?)(".*)/) {
			debugMapping('Component', 'lead', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Current values for user custom fields
		if ($line =~ /^(\s*<CustomFieldValue .*customfield=")(.*?)(".*stringvalue=")(.*?)(".*)/) {
			my ($a, $id, $b, $value, $c) = ($1, $2, $3, $4, $5);
			if (exists $customFieldIds->{$id}) {
				debugMapping("CustomFieldValue[id=$id]", 'stringvalue', $value, mapKey($value)) if $VERBOSE >= 4;
				print OUT $a, $id, $b, mapKey($value), $c, "\n";
			} else {
				# We didn't make a record for this one, so it isn't a user picker.  No touchie!
				print "$currentFile:      CustomFieldValue[id=$id]: Not mapped\n" if $VERBOSE >= 5;
				print OUT $line;
			}
			next;
		}

		# Last person to edit a draft workflow
		if ($line =~ /^(\s*<DraftWorkflowScheme .*lastModifiedUser=")(.*?)(".*)/) {
			debugMapping('DraftWorkflowScheme', 'lastModifiedUser', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# External data storage.  Used to be used for preferences but isn't anymore.
		# No idea what's here now, but we'd better deal with it.
		if ($line =~ /^(\s*<ExternalEntity .*name=")(.*?)(".*)/) {
			debugMapping('ExternalEntity', 'name', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Favourites that this user has marked
		if ($line =~ /^(\s*<FavouriteAssociations .*username=")(.*?)(".*)/) {
			debugMapping('FavouriteAssociations', 'username', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# File attachment uploader
		if ($line =~ /^(\s*<FileAttachment .*author=")(.*?)(".*)/) {
			debugMapping('FileAttachment', 'author', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Filter subscription users
		if ($line =~ /^(\s*<FilterSubscription .*username=")(.*?)(".*)/) {
			debugMapping('FilterSubscription', 'username', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Current assignee and reporter for an issue
		if ($line =~ /<Issue /) {
			if ($line =~ /^(.* assignee=")(.*?)(".*)/) {
				debugMapping('Issue', 'assignee', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			if ($line =~ /^(.* reporter=")(.*?)(".*)/) {
				debugMapping('Issue', 'reporter', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			print OUT $line;
			next;
		}

		# Notification target (Single_User only)
		if ($line =~ /^(\s*<Notification .*type="Single_User".*parameter=")(.*?)(".*)/) {
			debugMapping('Notification[type=Single_User]', 'parameter', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Workflow transition stuff
		if ($line =~ /<(OSCurrentStep) / or $line =~ /<(OSHistoryStep) /) {
			my $entity = $1;
			if ($line =~ /^(.* owner=")(.*?)(".*)/) {
				debugMapping($entity, 'owner', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			if ($line =~ /^(.* caller=")(.*?)(".*)/) {
				debugMapping($entity, 'caller', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			print OUT $line;
			next;
		}

		# OSUser doesn't have anything in it

		# Portal page owner
		if ($line =~ /^(\s*<PortalPage .*username=")(.*?)(".*)/) {
			debugMapping('PortalPage', 'username', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Project lead
		if ($line =~ /^(\s*<Project .*lead=")(.*?)(".*)/) {
			debugMapping('Project', 'lead', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Project Role Actor with a single user target
		# <ProjectRoleActor id="10010" pid="10001" projectroleid="10002" roletype="atlassian-user-role-actor" roletypeparameter="admin"/>
		if ($line =~ /^(\s*<ProjectRoleActor .*roletype="atlassian-user-role-actor".*roletypeparameter=")(.*?)(".*)/) {
			debugMapping('ProjectRoleActor[type=atlassian-user-role-actor]', 'roletypeparameter', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Remember Me tokens
		if ($line =~ /^(\s*<RememberMeToken .*username=")(.*?)(".*)/) {
			debugMapping('RememberMeToken', 'username', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Issue security scheme with a "user" target
		if ($line =~ /^(\s*<SchemeIssueSecurities .*type="user".*parameter=")(.*?)(".*)/) {
			debugMapping('SchemeIssueSecurities[type=user]', 'parameter', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Permission scheme with a "user" target
		if ($line =~ /^(\s*<SchemePermissions .*type="user".*parameter=")(.*?)(".*)/) {
			debugMapping('SchemePermissions[type=user]', 'parameter', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# The owner and/or user of a saved search request (filter)
		if ($line =~ /<SearchRequest /) {
			if ($line =~ /^(.* author=")(.*?)(".*)/) {
				debugMapping('SearchRequest', 'author', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			if ($line =~ /^(.* user=")(.*?)(".*)/) {
				debugMapping('SearchRequest', 'user', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			print OUT $line;
			next;
		}

		# Trusted app link
		if ($line =~ /<TrustedApplication /) {
			if ($line =~ /^(.* createdBy=")(.*?)(".*)/) {
				debugMapping('Action', 'createdBy', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			if ($line =~ /^(.* updatedBy=")(.*?)(".*)/) {
				debugMapping('Action', 'updatedBy', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			print OUT $line;
			next;
		}

		# The owner of some data associated to the user, such as votes and watches
		if ($line =~ /^(\s*<UserAssociation .*sourceName=")(.*?)(".*)/) {
			debugMapping('UserAssociation', 'sourceName', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# Recently used items for suggested completions when a user is prompted for something.
		# Need to map the user that this info belongs to.  For 'UsedUser' references, the target
		# entityId is also a user that needs to be mapped.
		if ($line =~ /^(\s*<UserHistoryItem .*type=")(.*?)(".*entityId=")(.*?)(".*username=")(.*?)(".*)/) {
			my ($a, $type, $b, $entityId, $c, $userKey, $d) = ($1, $2, $3, $4, $5, $6, $7);
			if ($VERBOSE >= 4) {
				debugMapping("UserHistoryItem[type=$type]", 'username', $userKey, mapKey($userKey));
				debugMapping("UserHistoryItem[type=$type]", 'entityId', $entityId, mapKey($entityId)) if $type eq 'UsedUser';
			}
			$entityId = mapKey($entityId) if $type eq 'UsedUser';
			print OUT $a, $type, $b, $entityId, $c, mapKey($userKey), $d, "\n";
			next;
		}

		# The creator of a workflow.  This doesn't actually seem to get used?
		if ($line =~ /^(\s*<Workflow .*creator=")(.*?)(".*)/) {
			debugMapping('Workflow', 'creator', $2, mapKey($2)) if $VERBOSE >= 4;
			print OUT $1, mapKey($2), $3, "\n";
			next;
		}

		# The author and updateauthor of a worklog
		if ($line =~ /<Worklog /) {
			if ($line =~ /^(.* author=")(.*?)(".*)/) {
				debugMapping('Worklog', 'author', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			if ($line =~ /^(.* updateauthor=")(.*?)(".*)/) {
				debugMapping('Worklog', 'updateauthor', $2, mapKey($2)) if $VERBOSE >= 4;
				$line = $1 . mapKey($2) . $3 . "\n";
			}
			print OUT $line;
			next;
		}

		# Nothing we care about.  Spit it out as-is.
		print OUT $line;

		# At this verbosity level and higher, we bother to look for occurrances
		# of the usernames that we were prepared to map but didn't have a rule
		# that applied for this line.  <Membership/> and <User/> are fine; we
		# know that they have the current username only; they get updated explicitly
		# when we rename a user.  That is, they deliberately still have the username
		# rather than a key.
		if ($VERBOSE >= 2) {
			next if $line =~ /<Membership /;
			next if $line =~ /<User /;
			foreach my $name (keys %$name2key) {
				# Too many things have "user" as a type for us to bother reporting it
				# when it was coincidentally used as a username.
				next if $name eq 'user';
				my $n = '"' . $name . '"';
				warn "WARNING: UNMAPPED: \"$name\": $line" if index($line, $n) >= 0;
			}
		}
	}

	die "UNTERMINATED ENTITY:  $saved\n" if defined $saved;

	close IN;
	close OUT;

	unlink $currentFile or die "$currentFile: unlink: $!\n";

	# Warning: This won't work across filesystems on most operating systems.
	rename '.tmpfile.xml', $currentFile or die "$currentFile: rename from .tmpfile.xml: $!";
	print "$currentFile: Done.\n";
}


sub parseCommandLineOptions(@) {
	while (@_) {
		my $arg = $_[0];
		last unless $arg =~ /^-/;
		shift;
		last if $arg eq '--';

		if ($arg =~ /^-(v+)$/) {
			$VERBOSE += length($1);
			next;
		}

		warn "Unrecognized option: $arg\n";
		usage();
	}
	unless (@_) {
		warn "No targets specified.\n";
		usage();
	}
	return @_;
}

sub main(@) {
	my $targets = findAllTargets(parseCommandLineOptions(@_));
	unless (@$targets) {
		warn "No targets found.\n";
		usage();
	}
	foreach my $file (@$targets) {
		processFile($file);
	}
}

sub usage() {
	die <<"@@EOF";
Usage: $^X $0 [-v[v[...]] [--] target [target2 [...]]

  -v[v[...]]
	Increases the output verbosity (may be specified multiple times)

  target
	Either an XML file or a directory to recursively search for XML files
@@EOF
}

main(@ARGV);

