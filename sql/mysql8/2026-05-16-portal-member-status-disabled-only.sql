-- Convert legacy self-registration review status to the two-state member model.
-- Run before deploying code that removes MemberUserStatus.PENDING_REVIEW.
UPDATE portal_member_user
SET status = 'DISABLED'
WHERE status = 'PENDING_REVIEW';
