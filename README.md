# Voxel Engine 

## Video
[![https://imgur.com/gpvCbwj.png](https://imgur.com/gpvCbwj.png)](http://www.youtube.com/watch?v=eg5247_GSZ0 "Img1")

## Implementation

All voxels are stored in a modified octree, with 64 nodes instead of 8. This tree is populated with voxels. Next, another form of this tree is created to store 64x64x64 chunks of voxels, as VAO bindings. During this stage, all completely surrounded voxels are not included in the mesh. When updates are made to a chunk, this process is repeated solely for that chunk. All cubes are rendered with instanced rendering, and as such, each chunk only contains one cube mesh, and (up to) 4096 offsets and colors. 

Before rendering, chunks are frustum culled. This process starts from the camera, and increments 32 voxels outwards, sampling every 32 voxels (in the up and right directions) from the top left of the current frustum plane. This process continues until it has reached its max depth (32 iterations), or until there are ~1,500,000 voxels in the meshes already gathered.

Voxel removal is a ray cast from the camera to the nearest voxel, from which a radius of 2 voxels are removed. The 8 corners of this removal are checked, and the corresponding chunks are edited and updated in the tree.
